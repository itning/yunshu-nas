import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NzMessageService} from "ng-zorro-antd/message";
import {environment} from "../../../../../environments/environment";

@Component({
    selector: 'app-log',
    templateUrl: './log.component.html',
    styleUrls: ['./log.component.scss'],
    standalone: false
})
export class LogComponent implements OnInit, OnDestroy {

  @ViewChild('scrollBottom')
  private scrollBottom: ElementRef;

  private websocket: WebSocket;

  logData: string[] = [];

  autoScroll: boolean = true;

  constructor(private message: NzMessageService) {
  }

  ngOnInit(): void {
    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
      if (location.protocol === 'http:') {
        this.websocket = new WebSocket(`ws://${new URL(environment.backEndUrl).host}/log`);
      } else {
        this.websocket = new WebSocket(`wss://${new URL(environment.backEndUrl).host}/log`);
      }
      console.log("link success")
    } else {
      this.message.error('Not support websocket');
    }

    //连接发生错误的回调方法
    this.websocket.onerror = () => {
      this.setMessageInnerHTML("error link to log server");
    };

    //连接成功建立的回调方法
    this.websocket.onopen = (event) => {
      this.setMessageInnerHTML("success link to log server");
    };

    //接收到消息的回调方法
    this.websocket.onmessage = (event) => {
      this.setMessageInnerHTML(event.data);
    };

    //连接关闭的回调方法
    this.websocket.onclose = () => {
      this.setMessageInnerHTML("log server link is closed");
    };

    //监听窗口关闭事件，当窗口关闭时，主动去关闭websocket连接，防止连接还没断开就关闭窗口，server端会抛异常。
    window.onbeforeunload = () => {
      if (this.websocket) {
        this.websocket.close();
      }
    };
  }

  ngOnDestroy(): void {
    if (this.websocket) {
      this.websocket.close();
    }
  }

  private setMessageInnerHTML(log: string): void {
    const strArray = log.split("\n");
    for (let s = 0; s < strArray.length; s++) {
      if (strArray[s] !== '') {
        this.logData.push(strArray[s]);
      }
    }
    if (this.autoScroll) {
      setTimeout(() => window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'}), 100);
    }
  }

  clearMsg(): void {
    this.logData = [];
  }
}
