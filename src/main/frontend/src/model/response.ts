import { AxiosResponse } from 'axios';
import { ServerMessage } from './error';

export interface ServerResponse<R> {
  /**
   * Array of server messages
   */
  messages: ServerMessage[];
  /**
   * Response payload
   */
  result: R;
  /**
   * True if not message with level higher than warning is present.
   * An operation may be successful and still contain messages with
   * level {@link EnumLevel#INFO} or {@link EnumLevel#WARN}
   */
  success: boolean;
}

export type AxiosServerResponse<R> = AxiosResponse<ServerResponse<R>>;