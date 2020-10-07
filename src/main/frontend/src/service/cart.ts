import Api from '@/service/api';

import { AxiosServerResponse, ServerResponse } from '@/model/response';
import { AxiosResponse } from 'axios';
import { Cart, CartAddItemCommand } from '@/model/cart';

export default class CartApi extends Api {
  constructor() {
    super({ withCredentials: true });
  }

  public async getCart(): Promise<ServerResponse<Cart>> {
    const url = '/action/cart';

    return this.get<ServerResponse<Cart>>(url)
      .then((response: AxiosResponse<ServerResponse<Cart>>) => {
        const { data: serverResponse } = response;

        return serverResponse;
      });
  }

  public async addItem(command: CartAddItemCommand): Promise<ServerResponse<Cart>> {
    const url = '/action/cart';

    return this.post<CartAddItemCommand, ServerResponse<Cart>>(url, command)
      .then((response: AxiosResponse<ServerResponse<Cart>>) => {
        const { data: serverResponse } = response;

        return serverResponse;
      });
  }

  public async removeItem(id: string): Promise<ServerResponse<Cart>> {
    const url = `/action/cart/${id}`;

    return this.delete<ServerResponse<Cart>>(url)
      .then((response: AxiosServerResponse<Cart>) => {
        const { data } = response;

        return data;
      });
  }

  public async clear(): Promise<ServerResponse<Cart>> {
    const url = '/action/cart';

    return this.delete<ServerResponse<Cart>>(url)
      .then((response: AxiosResponse<ServerResponse<Cart>>) => {
        const { data: serverResponse } = response;

        return serverResponse;
      });
  }
}
