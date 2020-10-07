export interface PageRequest {
  /*
   * Page index. Page index is 0-based and cannot be a negative number
   */
  page: number;
  /*
   * Page size. Page size must be greater than zero
   */
  size: number;
}

export interface QueryResultPage<R> {
  /*
   * Page request options
   */
  pageRequest: PageRequest;
  /*
  * Total number of items
  */
  count: number;
  /*
   * Items for the current page
   */
  items: R[];
}