export interface AuthResult {
  csrfHeader: string;
  csrfToken: string;
}

export interface LoginResult extends AuthResult { }

export interface LogoutResult extends AuthResult { }
