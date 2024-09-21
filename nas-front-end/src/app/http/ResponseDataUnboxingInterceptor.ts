import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse,
  HttpStatusCode
} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import {Injectable} from '@angular/core';
import {map} from 'rxjs/operators';
import {RestModel} from './model/RestModel';
import {NzMessageService} from "ng-zorro-antd/message";

/**
 * <p>响应拆箱拦截转换
 * <p>从RestModel转换为data:any
 */
@Injectable()
export class ResponseDataUnboxingInterceptor implements HttpInterceptor {
  constructor(private message: NzMessageService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      map((event: HttpEvent<any>) => {
        if (event instanceof HttpResponse && event.status !== 204) {
          // 获取响应
          // noinspection TypeScriptValidateTypes
          const httpResponse: HttpResponse<RestModel<any>> = (event as HttpResponse<RestModel<any>>);
          // 获取RestModel中data
          if (HttpStatusCode.Ok !== httpResponse.status && HttpStatusCode.Created !== httpResponse.status) {
            console.error(`响应出错：${JSON.stringify(httpResponse.body)}`);
            console.error(httpResponse);
            this.message.error(httpResponse.body.msg);
          }
          if (!httpResponse.body) {
            return event;
          }
          const data: any = httpResponse.body.data;
          // clone and return
          return httpResponse.clone<any>({
            body: data
          });
        }
        return event;
      }),
      catchError((error: HttpErrorResponse) => {
        // 检查是否是 HttpErrorResponse 类型
        if (error instanceof HttpErrorResponse) {
          console.error(`响应出错：${JSON.stringify(error.error)}`);
          console.error(error);
          if (error.status === 0) {
            this.message.error(error.statusText);
          } else {
            this.message.error(JSON.stringify(error.error));
          }
        }
        return throwError(() => new Error(error.error));
      })
    );
  }
}
