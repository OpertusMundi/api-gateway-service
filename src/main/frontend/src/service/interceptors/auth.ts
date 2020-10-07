import { AxiosRequestConfig } from 'axios';

import store from '@/store/modules/user.module';

export default function csrfRequestInterceptor(value: AxiosRequestConfig): AxiosRequestConfig | Promise<AxiosRequestConfig> {
  const { token = null } = store.state.auth;

  // Append authentication token only if one exists
  if (token) {
    const authHeader = {
      Authorization: `Bearer ${token}`,
    };
    return {
      ...value,
      headers: value.headers ? { ...value.headers, ...authHeader } : authHeader,
    };
  }

  // Ignore authentication token
  return {
    ...value,
  };
}
