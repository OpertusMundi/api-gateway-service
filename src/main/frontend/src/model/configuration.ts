export enum EnumAuthProvider {
  Forms = 'Forms',
  Google = 'Google',
  GitHub = 'GitHub',
  OpertusMundi = 'OpertusMundi',
}

export interface Configuration {
  authProviders: EnumAuthProvider[];
}
