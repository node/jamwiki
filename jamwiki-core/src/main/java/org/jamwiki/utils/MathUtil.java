/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.util.EmptyStackException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.congrace.exp4j.CustomFunction;
import de.congrace.exp4j.CustomOperator;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.InvalidCustomFunctionException;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

/**
 * Math utility functions.
 */
public abstract class MathUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(MathUtil.class.getName());
	private static final String MATH_NUMBER_PATTERN_STRING = "((?:\\-)?[0-9]*(?:\\.)?[0-9]+)";
	private static final String MATH_FUNCTION_PATTERN_STRING = "(abs|acos|asin|atan|cbrt|ceil|cos|cosh|e|exp|floor|ln|log|round|sin|sinh|sqrt|tan|tanh|trunc)(?:\\s)*" + MATH_NUMBER_PATTERN_STRING;
	private static final Pattern MATH_FUNCTION_PATTERN = Pattern.compile(MATH_FUNCTION_PATTERN_STRING);
	private static final String MATH_MOD_PATTERN_STRING = "([^A-Za-z]+?)mod([^A-Za-z]+?)";
	private static final Pattern MATH_MOD_PATTERN = Pattern.compile(MATH_MOD_PATTERN_STRING);
	private static CustomFunction FUNCTION_LN;
	private static CustomFunction FUNCTION_ROUND;
	private static CustomFunction FUNCTION_TRUNC;
	private static CustomOperator OPERATOR_GT;
	private static CustomOperator OPERATOR_GTE;
	private static CustomOperator OPERATOR_LT;
	private static CustomOperator OPERATOR_LTE;

	static {
		MathUtil.initializeCustomFunctions();
		MathUtil.initializeCustomOperators();
	}

	/**
	 * Process a mathematical expression of the form "1 + 3 / 2" and return
	 * the result as a double.
	 *
	 * @param expr A mathematical expression of the form "1 + 3 / 2".
	 * @return The result of the expression as a double.
	 * @throws IllegalArgumentException Thrown if the expression has errors
	 *  such as a missing parentheses or if there are invalid characters
	 *  in the function.
	 */
	public static double evaluateExpression(String expr) throws IllegalArgumentException {
		// support "mod" as a synonym to "%"
		Matcher matcher = MATH_MOD_PATTERN.matcher(expr);
		expr = matcher.replaceAll("$1%$2");
		// allow usage of syntax such as "round1.2" by using a regular expression to
		// add parentheses to function expressions - example "round(1.2)".
		matcher = MATH_FUNCTION_PATTERN.matcher(expr);
		expr = matcher.replaceAll("$1($2)");
		// process the updated expression
		try {
			double result = new ExpressionBuilder(expr)
					.withCustomFunction(FUNCTION_LN)
					.withCustomFunction(FUNCTION_ROUND)
					.withCustomFunction(FUNCTION_TRUNC)
					.withOperation(OPERATOR_GT)
					.withOperation(OPERATOR_GTE)
					.withOperation(OPERATOR_LT)
					.withOperation(OPERATOR_LTE)
					.withVariable("e", Math.E)
					.withVariable("pi", Math.PI)
					.build()
					.calculate();
			if (result == Double.NEGATIVE_INFINITY || result == Double.POSITIVE_INFINITY) {
				throw new IllegalArgumentException("/ 0");
			}
			return result;
		} catch (UnknownFunctionException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		} catch (UnparsableExpressionException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(expr);
		} catch (EmptyStackException e) {
			throw new IllegalArgumentException(expr);
		}
	}

	/**
	 *
	 */
	private static void initializeCustomFunctions() {
		try {
			FUNCTION_LN = new CustomFunction("ln") {
				public double applyFunction(double[] values) {
					return Math.log(values[0]);
				}
			};
			FUNCTION_ROUND = new CustomFunction("round") {
				public double applyFunction(double[] values) {
					return Math.round(values[0]);
				}
			};
			FUNCTION_TRUNC = new CustomFunction("trunc") {
				public double applyFunction(double[] values) {
					return (values[0] >= 0) ? Math.floor(values[0]) : Math.ceil(values[0]);
				}
			};
		} catch (InvalidCustomFunctionException e) {
			logger.error("Failure while initializing MathUtil", e);
		}
	}

	/**
	 *
	 */
	private static void initializeCustomOperators() {
		OPERATOR_GT = new CustomOperator(">", true, 4, 2) {
			protected double applyOperation(double[] values) {
				return (values[0] > values[1]) ? 1d : 0d;
			}
		};
		OPERATOR_GTE = new CustomOperator(">=", true, 4, 2) {
			protected double applyOperation(double[] values) {
				return (values[0] >= values[1]) ? 1d : 0d;
			}
		};
		OPERATOR_LT = new CustomOperator("<", true, 4, 2) {
			protected double applyOperation(double[] values) {
				return (values[0] < values[1]) ? 1d : 0d;
			}
		};
		OPERATOR_LTE = new CustomOperator("<=", true, 4, 2) {
			protected double applyOperation(double[] values) {
				return (values[0] <= values[1]) ? 1d : 0d;
			}
		};
	}
}
