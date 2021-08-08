package jlox;

import java.util.List;
import static jlox.TokenType.*;

class Parser {

	private static class ParseError extends RuntimeException {
	}

	private final List<Token> tokens;
	private int current = 0;

	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	Expr parse() {
		try {
			return expression();
		} catch (ParseError error) {
			return null;
		}
	}

	/**
	 * expression → equality ;
	 */
	private Expr expression() {
		return equality();
	}

	/**
	 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
	 */
	private Expr equality() {
		Expr expr = comparison();

		while (match(BANG_EQUAL, EQUAL_EQUAL)) {
			Token operator = previous();
			Expr right = comparison();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * See if the current token has any of the given types
	 */
	private boolean match(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}

		return false;
	}

	/**
	 * @return true if the current token is of the given type.
	 */
	private boolean check(TokenType type) {
		if (isAtEnd()) {
			return false;
		}
		return peek().type == type;
	}

	/**
	 * Consumes the current token and returns it
	 * 
	 * @return current token
	 */
	private Token advance() {
		if (!isAtEnd()) {
			current++;
		}
		return previous();
	}

	/**
	 * Checks if we’ve run out of tokens to pars
	 */
	private boolean isAtEnd() {
		return peek().type == EOF;
	}

	/**
	 * @return current token we have yet to consume
	 */
	private Token peek() {
		return tokens.get(current);
	}

	/**
	 * @return most recently consumed token
	 */
	private Token previous() {
		return tokens.get(current - 1);
	}

	/**
	 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
	 */
	private Expr comparison() {
		Expr expr = term();

		while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
			Token operator = previous();
			Expr right = term();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * term → factor ( ( "-" | "+" ) factor )* ;
	 */
	private Expr term() {
		Expr expr = factor();

		while (match(MINUS, PLUS)) {
			Token operator = previous();
			Expr right = factor();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * factor → unary ( ( "/" | "*" ) unary )* ;
	 */
	private Expr factor() {
		Expr expr = unary();

		while (match(SLASH, STAR)) {
			Token operator = previous();
			Expr right = unary();
			expr = new Expr.Binary(expr, operator, right);
		}

		return expr;
	}

	/**
	 * unary → ( "!" | "-" ) unary | primary ;
	 */
	private Expr unary() {
		if (match(BANG, MINUS)) {
			Token operator = previous();
			Expr right = unary();
			return new Expr.Unary(operator, right);
		}

		return primary();
	}

	/**
	 * primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
	 */
	private Expr primary() {
		if (match(FALSE)) {
			return new Expr.Literal(false);
		}

		if (match(TRUE)) {
			return new Expr.Literal(true);
		}

		if (match(NIL)) {
			return new Expr.Literal(null);
		}

		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().literal);
		}

		if (match(LEFT_PAREN)) {
			Expr expr = expression();
			consume(RIGHT_PAREN, "Expect ')' after expression.");
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Expect expression.");
	}

	/**
	 * checks to see if the next token is of the expected type
	 */
	private Token consume(TokenType type, String message) {
		if (check(type)) {
			return advance();
		}

		throw error(peek(), message);
	}

	private ParseError error(Token token, String message) {
		Lox.error(token, message);
		return new ParseError();
	}

	/**
	 * It discards tokens until it thinks it has found a statement boundary. After
	 * catching a ParseError, we’ll call this and then we are hopefully back in
	 * sync.
	 */
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
			if (previous().type == SEMICOLON)
				return;

			switch (peek().type) {
				case CLASS:
				case FUN:
				case VAR:
				case FOR:
				case IF:
				case WHILE:
				case PRINT:
				case RETURN:
					return;
			}

			advance();
		}
	}
}