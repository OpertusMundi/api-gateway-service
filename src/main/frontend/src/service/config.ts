import Api from '@/service/api';

import { Configuration } from '@/model/configuration';
import { AxiosServerResponse, ServerResponse } from '@/model/response';

export default class ConfigurationApi extends Api {
  constructor() {
    super({ withCredentials: true });
  }

  public async getConfiguration(locale = 'en'): Promise<ServerResponse<Configuration>> {
    const url = `/action/configuration/${locale}`;

    return this.get<ServerResponse<Configuration>>(url)
      .then((response: AxiosServerResponse<Configuration>) => {
        const { data } = response;

        return data;
      });
  }
}
