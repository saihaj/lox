import { TokenType } from "./TokenType.ts";

export class Token {
  type: TokenType;
  lexeme: string;
  literal: unknown | null;
  line: number;

  constructor(
    type: TokenType,
    lexeme: string,
    literal: unknown | null,
    line: number,
  ) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  toString() {
    return this.type + " " + this.lexeme + " " + this.literal;
  }
}
