import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'musicTypeReadable',
    standalone: false
})
export class MusicTypeReadablePipe implements PipeTransform {

  transform(value: number): string {
    switch (value) {
      case 1:
        return 'flac';
      case 2:
        return 'mp3';
      case 3:
        return 'wav';
      case 4:
        return 'aac';
      default:
        return `未知：${value}`;
    }
  }

}
