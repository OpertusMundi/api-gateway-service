import axios, {
  AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError,
} from 'axios';

import authRequestInterceptor from '@/service/interceptors/auth';
import csrfRequestInterceptor from '@/service/interceptors/csrf';

export function handleError<T>(error: AxiosError): Promise<AxiosResponse<T>> {
  if (error.response) {
    // The request was made and the server responded with a status code
    // that falls out of the range of 2xx
    console.log(error.response);
  } else if (error.request) {
    // The request was made but no response was received
    // `error.request` is an instance of XMLHttpRequest in the browser and an instance of
    // http.ClientRequest in node.js
    console.log(error.request);
  } else {
    // Something happened in setting up the request that triggered an Error
    console.log('Error', error.message);
  }
  // The request configuration
  console.log(error.config);

  throw error;
}

export default class Api {
  private api: AxiosInstance;

  public constructor(config: AxiosRequestConfig = { withCredentials: true }) {
    this.api = axios.create(config);

    // Injects CSRF Token when production profile is enabled (Cookie based authentication)
    this.api.interceptors.request.use(csrfRequestInterceptor);
    // Injects JWT token when development profile is enabled (Token based authentication);
    this.api.interceptors.request.use(authRequestInterceptor);
  }

  protected get<T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.api.get<T>(url, config)
      .catch((error: AxiosError) => handleError(error));
  }

  protected delete<T>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.api.delete<T>(url, config)
      .catch((error: AxiosError) => handleError(error));
  }

  protected post<R, T>(url: string, data?: R | null, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.api.post<T>(url, data, config)
      .catch((error: AxiosError) => handleError(error));
  }

  protected put<R, T>(url: string, data?: R | null, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return this.api.put<T>(url, data, config)
      .catch((error: AxiosError) => handleError(error));
  }

  protected submit<T = any>(url: string, data?: FormData, config: AxiosRequestConfig = {}): Promise<AxiosResponse<T>> {
    const headers = {
      'Content-Type': 'application/x-www-form-urlencoded',
    };

    const effectiveConfig = {
      ...config,
      headers: config.headers ? { ...config.headers, ...headers } : { ...headers },
    };

    return this.api.post<T>(url, data, effectiveConfig)
      .catch((error: AxiosError) => handleError(error));
  }
}
