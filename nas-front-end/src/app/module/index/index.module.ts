import {NgModule} from '@angular/core';
import {SharedModule} from '../shared/shared.module';
import {IndexComponent} from './component/index/index.component';

@NgModule({
  declarations: [IndexComponent],
  imports: [
    SharedModule
  ]
})
export class IndexModule {
}
