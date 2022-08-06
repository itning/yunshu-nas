import {Pageable} from './Pageable';
import {Sort} from './Sort';

export class Page<DATA> {
  last: boolean;
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  numberOfElements: number;
  first: boolean;
  empty: boolean;
  pageable: Pageable;
  sort: Sort;
  content: DATA[];
}
