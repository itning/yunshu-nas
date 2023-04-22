import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {IndexComponent} from "./module/index/component/index/index.component";
import {ListComponent as MusicList} from "./module/music/component/list/list.component";
import {ListComponent as VideoList} from "./module/video/component/list/list.component";
import {AddComponent} from "./module/music/component/add/add.component";
import {EditComponent} from "./module/music/component/edit/edit.component";
import {PlayComponent} from "./module/video/component/play/play.component";
import {LogComponent} from "./module/index/component/log/log.component";
import {DownloadComponent} from "./module/video/component/download/download.component";
import {SettingComponent} from "./module/index/component/setting/setting.component";

const routes: Routes = [
  {
    path: '',
    component: IndexComponent,
    children: [
      {path: '', redirectTo: 'video/list', pathMatch: 'full'},
      {path: 'video/list', component: VideoList},
      {path: 'video/list/:path', component: VideoList},
      {path: 'video/play/:path', component: PlayComponent},
      {path: 'music/list', component: MusicList},
      {path: 'music/add', component: AddComponent},
      {path: 'music/edit/:id', component: EditComponent},
      {path: 'log', component: LogComponent},
      {path: 'download', component: DownloadComponent},
      {path: 'setting', component: SettingComponent},
      {path: '**', redirectTo: 'video/list', pathMatch: 'full'}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
