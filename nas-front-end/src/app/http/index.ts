import { HTTP_INTERCEPTORS } from '@angular/common/http';
import {Provider} from '@angular/core';
import {ResponseDataUnboxingInterceptor} from './ResponseDataUnboxingInterceptor';

export const httpInterceptorProviders: Provider[] = [
  {provide: HTTP_INTERCEPTORS, useClass: ResponseDataUnboxingInterceptor, multi: true}
]
