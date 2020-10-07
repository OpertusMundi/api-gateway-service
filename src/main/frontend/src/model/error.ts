export enum EnumLevel {
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
  CRITICAL = 'CRITICAL',
}

export interface ServerMessage {
  code: string;
  level: EnumLevel;
  description: string;
}
