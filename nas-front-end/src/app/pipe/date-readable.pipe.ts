import {Pipe, PipeTransform} from '@angular/core';
import * as dayjs from "dayjs";

@Pipe({
    name: 'dateReadable',
    standalone: false
})
export class DateReadablePipe implements PipeTransform {

  transform(value: string): string {
    return dayjs(value).format('YYYY-MM-DD HH:mm:ss');
  }

}
