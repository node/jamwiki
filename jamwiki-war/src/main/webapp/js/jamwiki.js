var JAMWiki = JAMWiki || {};
if (!Function.bind) {
	// allow binding of a function to a specific scope.  Usage: myFunction.bind(this, args).
	Function.prototype.bind = function(scope) {
		var _function = this;
		// trim the argument array (if any) to remove the scope argument
		var args = new Array();
		for (var i = 1; i < arguments.length; i++) {
			args[i - 1] = arguments[i];
		}
		return function() {
			return _function.apply(scope, args);
		}
	}
}
JAMWiki.Editor = function() {
	var alertText;
	var clientPC = navigator.userAgent.toLowerCase(); // Get client info
	var is_gecko = ((clientPC.indexOf('gecko')!=-1) && (clientPC.indexOf('spoofer')==-1)
					&& (clientPC.indexOf('khtml') == -1) && (clientPC.indexOf('netscape/7.0')==-1));
	var is_safari = ((clientPC.indexOf('AppleWebKit')!=-1) && (clientPC.indexOf('spoofer')==-1));
	var noOverwrite = false;
	function escapeQuotes(text) {
		var re = new RegExp("'","g");
		text = text.replace(re,"\\'");
		re = new RegExp('"',"g");
		text = text.replace(re,'&quot;');
		re = new RegExp("\\n","g");
		text = text.replace(re,"\\n");
		return text;
	}
	// apply tagOpen/tagClose to selection in textarea,
	// use sampleText instead of selection if there is none
	// copied and adapted from phpBB
	function insertTags(tagOpen, tagClose, sampleText) {
		var txtarea = document.editForm.contents;
		// IE
		if (document.selection  && !is_gecko) {
			var theSelection = document.selection.createRange().text;
			if (!theSelection) {
				theSelection=sampleText;
			}
			txtarea.focus();
			if (theSelection.charAt(theSelection.length - 1) == " ") {
				// exclude ending space char, if any
				theSelection = theSelection.substring(0, theSelection.length - 1);
				document.selection.createRange().text = tagOpen + theSelection + tagClose + " ";
			} else {
				document.selection.createRange().text = tagOpen + theSelection + tagClose;
			}
		// Mozilla
		} else if (txtarea.selectionStart || txtarea.selectionStart == '0') {
			var startPos = txtarea.selectionStart;
			var endPos = txtarea.selectionEnd;
			var scrollTop = txtarea.scrollTop;
			var myText = (txtarea.value).substring(startPos, endPos);
			if (!myText) {
				myText = sampleText;
			}
			if (myText.charAt(myText.length - 1) == " ") {
				// exclude ending space char, if any
				subst = tagOpen + myText.substring(0, (myText.length - 1)) + tagClose + " ";
			} else {
				subst = tagOpen + myText + tagClose;
			}
			txtarea.value = txtarea.value.substring(0, startPos) + subst + txtarea.value.substring(endPos, txtarea.value.length);
			txtarea.focus();
			var cPos = startPos + (tagOpen.length + myText.length + tagClose.length);
			txtarea.selectionStart = cPos;
			txtarea.selectionEnd = cPos;
			txtarea.scrollTop = scrollTop;
		// All others
		} else {
			var copy_alertText = alertText;
			var re1 = new RegExp("\\$1","g");
			var re2 = new RegExp("\\$2","g");
			copy_alertText = copy_alertText.replace(re1,sampleText);
			copy_alertText = copy_alertText.replace(re2,tagOpen+sampleText+tagClose);
			var text;
			if (sampleText) {
				text = prompt(copy_alertText);
			} else {
				text="";
			}
			if (!text) {
				text = sampleText;
			}
			text = tagOpen + text + tagClose;
			document.infoform.infobox.value = text;
			// in Safari this causes scrolling
			if (!is_safari) {
				txtarea.focus();
			}
			noOverwrite = true;
		}
		// reposition cursor if possible
		if (txtarea.createTextRange) {
			txtarea.caretPos = document.selection.createRange().duplicate();
		}
	}
	return {
		initButton: function(buttonId, tagOpen, tagClose, sampleText) {
			// we can't change the selection, so we show example texts
			// when moving the mouse instead, until the first button is clicked
			if (!document.selection && !is_gecko) {
				// filter backslashes so it can be shown in the infobox
				var re = new RegExp("\\\\n", "g");
				tagOpen = tagOpen.replace(re, "");
				tagClose = tagClose.replace(re, "");
			}
			document.getElementById(buttonId).onclick = insertTags.bind(JAMWiki.Editor, tagOpen, tagClose, sampleText);
		}
	};
}();
JAMWiki.UI = function() {
	function toggleToc(toggleLink, toggleTarget, hideLabel, showLabel) {
		if (toggleLink.innerHTML == hideLabel) {
			toggleLink.innerHTML = showLabel;
			JAMWiki.UI.addClass(toggleTarget, "hidden");
		} else {
			toggleLink.innerHTML = hideLabel;
			JAMWiki.UI.removeClass(toggleTarget, "hidden");
		}
		return false;
	}
	return {
		// assign a CSS class to an element if it is not already assigned
		addClass: function(element, cssClassName) {
			if (!JAMWiki.UI.hasClass(element, cssClassName)) {
				if (element.className.length > 0) {
					element.className += " " + cssClassName;
				} else {
					element.className = cssClassName;
				}
			}
		},
		// determine if an element currently has a CSS class
		hasClass: function(element, cssClassName) {
			var classNames = element.className.split(" ");
			for (var i=0; i < classNames.length; i++) {
				if (classNames[i] == cssClassName) {
					return true;
				}
			}
			return false;
		},
		// remove a CSS class from an element if it is assigned
		removeClass: function(element, cssClassName) {
			var classNames = element.className.split(" ");
			var updatedClassNames = "";
			for (var i=0; i < classNames.length; i++) {
				if (classNames[i] == cssClassName) {
					continue;
				}
				if (updatedClassNames.length > 0) {
					updatedClassNames += " ";
				}
				updatedClassNames += classNames[i];
			}
			element.className = updatedClassNames;
		},
		// remove a CSS class from an element if it is assigned, otherwise add it.
		toggleClass: function(element, cssClassName) {
			if (JAMWiki.UI.hasClass(element, cssClassName)) {
				JAMWiki.UI.removeClass(element, cssClassName);
			} else {
				JAMWiki.UI.addClass(element, cssClassName);
			}
		},
		// enable/disable radio buttons before or after the current element
		historyRadio: function(element, siblingName, disableLower) {
			// since revision numbers are chronological this code compares revision 
			// numbers to determine whether to display or hide a radio button
			var revisionId = parseInt(element.value);
			var siblings = document.getElementsByName(siblingName);
			for (var i = 0; i < siblings.length; i++) {
				// make sure the element is a radio button, if not skip it
				if (siblings[i].type != 'radio') {
					continue;
				}
				// now make the button visible or hidden as appropriate
				if (disableLower && parseInt(siblings[i].value) <= revisionId) {
					siblings[i].style.visibility = 'hidden';
				} else if (!disableLower && parseInt(siblings[i].value) >= revisionId) {
					siblings[i].style.visibility = 'hidden';
				} else {
					siblings[i].style.visibility = 'visible';
				}
			}
		},
		// allow expanding/collapsing sections
		initializeToggle: function(containerElement, toggleElement, expandedClass) {
			// close by default
			JAMWiki.UI.removeClass(containerElement, expandedClass);
			toggleElement.onclick = JAMWiki.UI.toggleClass.bind(JAMWiki.UI, containerElement, expandedClass);
		},
		// toggle the show/hide link in the TOC header
		initializeTocToggle: function(hideLabel, showLabel) {
			var toggleLink = document.getElementById("toggle-link");
			var toggleTarget = document.getElementById("toc-content")
			toggleLink.onclick = toggleToc.bind(this, toggleLink, toggleTarget, hideLabel, showLabel);
		}
	};
}();
JAMWiki.Tabs = function() {
	// Based on code by Matt Doyle http://www.elated.com/articles/javascript-tabs/.
	var tabLinks = new Array();
	var contentDivs = new Array();
	function showTab() {
		var tabLink = getFirstChildWithTagName(this, 'A');
		var selectedId = getHash(tabLink.getAttribute('href'));
		for (var id in contentDivs) {
			if (id == selectedId) {
				tabLinks[id].className = 'active';
				contentDivs[id].className = 'submenu-tab-item';
			} else {
				tabLinks[id].className = '';
				contentDivs[id].className = 'submenu-tab-item hidden';
			}
		}
		return false;
	}
	function getFirstChildWithTagName(element, tagName) {
		for (var i = 0; i < element.childNodes.length; i++) {
			if (element.childNodes[i].nodeName == tagName) {
				return element.childNodes[i];
			}
		}
	}
	function getHash(url) {
		var hashPos = url.lastIndexOf('#');
		return url.substring(hashPos + 1);
	}
	return {
		initializeTabs: function() {
			var tabNode = document.getElementById('tab_submenu');
			if (!tabNode) {
				return;
			}
			var tabListItems = tabNode.childNodes;
			for (var i = 0; i < tabListItems.length; i++) {
				if (tabListItems[i].nodeName == "LI") {
					var tabLink = getFirstChildWithTagName(tabListItems[i], 'A');
					var id = getHash(tabLink.getAttribute('href'));
					tabLinks[id] = tabListItems[i];
					contentDivs[id] = document.getElementById(id);
				}
			}
			var i = 0;
			var selectedId = 0;
			for (var id in tabLinks) {
				tabLinks[id].onclick = showTab;
				var tabLink = getFirstChildWithTagName(tabLinks[id], 'A');
				tabLink.onfocus = function() {
					// blur focus to avoid a link box around the text
					this.blur();
				};
				if (document.location.hash == '#' + id || i == 0) {
					selectedId = id;
				}
				i++;
			}
			tabLinks[selectedId].className = 'active';
			var i = 0;
			for (var id in contentDivs) {
				if (id != selectedId) {
					contentDivs[id].className = 'submenu-tab-item hidden';
				}
				i++;
			}
		}
	};
}();
JAMWiki.Admin = function() {
	// array of checkbox ID and corresponding text input ID
	var virtualWikiCheckboxArray = [
			['defaultRootTopicName', 'rootTopicName'],
			['defaultVirtualWikiSiteName', 'virtualWikiSiteName'],
			['defaultVirtualWikiLogoImageUrl', 'virtualWikiLogoImageUrl'],
			['defaultVirtualWikiMetaDescription', 'virtualWikiMetaDescription']
	];
	function toggleInputState(checkbox, input) {
		input.disabled = checkbox.checked;
		if (checkbox.checked) {
			input.value = checkbox.value;
		}
	}
	function toggleDisableOnChange(selectElement, enableValue, idElements, containerElement, expandedClass) {
		var disabled = (selectElement.options[selectElement.selectedIndex].value != enableValue);
		for (var i = 0; i < idElements.length; i++) {
			var disableElement = document.getElementById(idElements[i]);
			if (disableElement) {
				disableElement.disabled = disabled;
			}
		}
		if (disabled) {
			JAMWiki.UI.removeClass(containerElement, expandedClass);
		} else {
			JAMWiki.UI.addClass(containerElement, expandedClass);
		}
	}
	function sampleValuesUpdate(selectElement, driverElementId, urlElementId, sampleValues) {
		var databaseType = selectElement.options[selectElement.selectedIndex].value;
		var sampleDriver = ((sampleValues[databaseType]) ? sampleValues[databaseType][driverElementId] : "");
		var sampleUrl = ((sampleValues[databaseType]) ? sampleValues[databaseType][urlElementId] : "");
		document.getElementById(driverElementId).value = sampleDriver;
		document.getElementById(urlElementId).value = sampleUrl;
	}
	return {
		initializeVirtualWikiCheckboxes: function() {
			for (var i = 0; i < virtualWikiCheckboxArray.length; i++) {
				var checkbox = document.getElementById(virtualWikiCheckboxArray[i][0]);
				if (!checkbox) {
					// not available on this page
					break;
				}
				var input = document.getElementById(virtualWikiCheckboxArray[i][1]);
				checkbox.onclick = toggleInputState.bind(JAMWiki.Admin, checkbox, input);
				toggleInputState(checkbox, input);
			}
		},
		// toggle the disabled state of additional fields when a select element is changed
		toggleDisableOnSelect: function(selectElement, enableValue, idElements, containerElement, expandedClass) {
			toggleDisableOnChange(selectElement, enableValue, idElements, containerElement, expandedClass);
			selectElement.onchange = toggleDisableOnChange.bind(this, selectElement, enableValue, idElements, containerElement, expandedClass);
		},
		// populate database fields with appropriate sample values
		sampleDatabaseValues: function(selectElement, driverElementId, urlElementId, sampleValues) {
			selectElement.onchange = sampleValuesUpdate.bind(this, selectElement, driverElementId, urlElementId, sampleValues);
		}
	};
}();
window.onload = function() {
	JAMWiki.Tabs.initializeTabs();
	JAMWiki.Admin.initializeVirtualWikiCheckboxes();
}
