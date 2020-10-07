import { AxiosRequestConfig } from 'axios';

import store from '@/store/modules/user.module';

export default function csrfRequestInterceptor(value: AxiosRequestConfig): AxiosRequestConfig | Promise<AxiosRequestConfig> {
  const { token, header } = store.state.csrf;

  const required = ['POST', 'PUT', 'DELETE'].includes((value.method as string).toUpperCase());

  const csrfHeader = { [header || 'X-CSRF-TOKEN']: token };

  const result = token && required ? {
    ...value,
    headers: value.headers ? { ...value.headers, ...csrfHeader } : csrfHeader,
  } : {
    ...value,
  };

  return result;
}
