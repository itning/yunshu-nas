import {Pipe, PipeTransform} from '@angular/core';
import {Base64} from 'js-base64';

@Pipe({
    name: 'base64Decoder',
    standalone: false
})
export class Base64DecoderPipe implements PipeTransform {

  transform(value: string): string {
    return Base64.decode(value);
  }

}
