package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Lox
 * 
 * @apiNote Error code follow UNIX `sysexits.h`
 *          https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html
 */
public class Lox {
	private static final Interpreter interpreter = new Interpreter();
	static boolean hadError = false;
	static boolean hadRuntimeError = false;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	/**
	 * If you start jlox from the command line and give it a path to a file, it
	 * reads the file and executes it.
	 */
	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		// We’ll use this to ensure we don’t try to execute code that has a known error.
		if (hadError) {
			System.exit(65);
		}

		if (hadRuntimeError) {
			System.exit(70);
		}
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("> ");
			String line = reader.readLine();
			// To kill an interactive command-line app, you usually type Control-D. Doing so
			// signals an “end-of-file” condition to the program.
			if (line == null) {
				break;
			}
			run(line);
			// If the user makes a mistake, it shouldn’t kill their entire session.
			hadError = false;
		}
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		// Stop if there was a syntax error.
		if (hadError) {
			return;
		}

		interpreter.interpret(statements);
	}

	static void error(int line, String message) {
		report(line, "", message);
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}

	/**
	 * This reports an error at a given token. It shows the token’s location and the
	 * token itself.
	 */
	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "'", message);
		}
	}

	/**
	 * If a runtime error is thrown while evaluating the expression, interpret()
	 * catches it. This lets us report the error to the user and then gracefully
	 * continue.
	 */
	static void runtimeError(RuntimeError error) {
		System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}
}