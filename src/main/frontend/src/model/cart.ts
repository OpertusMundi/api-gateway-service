import { CatalogueItem } from '@/model/catalogue';
import { BasePricingModel } from '@/model/pricing-model';

export interface CartItem {
  /*
   * Cart item unique identifier
   */
  id: string;
  /*
   * Catalogue item
   */
  product: CatalogueItem;
  /*
   * Date added to the cart
   */
  addedAt: string;
  /*
   * Selected pricing model
   */
  pricingModel: BasePricingModel;
}

export interface Cart {
  /*
   * Selected items
   */
  items: CartItem[];
  /*
   * Applied discount coupons (for future use)
   */
  appliedCoupons: string[];
  /*
   * Total price including tax
   */
  totalPrice: number;
  /*
   * Total price excluding tax
   */
  totalPriceExcludingTax: number;
  /*
   * Total tax
   */
  taxTotal: number;
  /*
   * Currency of monetary values (EUR)
   */
  currency: string;
  /*
   * Number of items in the cart
   */
  totalItems: number;
  /*
   * Cart creation date in ISO format e.g. 2020-06-10T16:01:04.991+03:00
   */
  createdAt: string;
  /*
   * Cart most recent update date in ISO format e.g. 2020-06-10T16:01:04.991+03:00
   */
  modifiedAt: string;
}

export interface CartAddItemCommand {
  /*
   * Catalogue asset unique id
   */
  productId: string;
  /*
   * Pricing model unique id (the id must be one from the supported pricing models
   * returned by the catalogue, for the specific asset selected by the productId
   * property)
   */
  pricingModelId: string;
}
