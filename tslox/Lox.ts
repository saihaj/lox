import { readLines } from "https://deno.land/std@0.76.0/io/bufio.ts";
import { Scanner } from "./Scanner.ts";

export class Lox {
  private static hadError = false;

  private static run(source: string): void {
    const scanner = new Scanner(source);
    const tokens = scanner.scanTokens();

    // For now, just print the tokens.
    tokens.forEach((token) => console.log(token.toString()));
  }

  private static report(line: number, where: string, message: string): void {
    console.log(`[${line} line] Error ${where} : ${message}`);
    this.hadError = true;
  }

  static error(line: number, message: string): void {
    this.report(line, "", message);
  }

  /**
   * If you start tslox from the command line and give it a path to a file, it
   * reads the file and executes it.
   */
  private static async runFile(path: string) {
    const text = await Deno.readTextFile(path);
    this.run(text);

    if (this.hadError) {
      Deno.exit(65);
    }
  }

  private static async runPrompt() {
    for await (const line of readLines(Deno.stdin)) {
      this.run(line);
      this.hadError = false;
    }
  }

  public static async main() {
    if (Deno.args.length > 1) {
      console.log("Usage: tslox [script]");
      Deno.exit(64);
    } else if (Deno.args.length == 1) {
      await this.runFile(Deno.args[0]);
    } else {
      await this.runPrompt();
    }
  }
}

// Start the program
Lox.main()
  .then((a) => console.log(a))
  .catch((err) => console.error(err));
