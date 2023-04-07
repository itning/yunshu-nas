import {NgModule} from '@angular/core';
import {SharedModule} from '../shared/shared.module';
import {IndexComponent} from './component/index/index.component';
import { LogComponent } from './component/log/log.component';
import { SettingComponent } from './component/setting/setting.component';

@NgModule({
  declarations: [IndexComponent, LogComponent, SettingComponent],
  imports: [
    SharedModule
  ]
})
export class IndexModule {
}
