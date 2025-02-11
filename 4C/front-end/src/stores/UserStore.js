import {defineStore} from "pinia";

//export 导出
// 创建的存储可以看作是一个包含状态变量和方法的对象。这个对象可以在整个应用程序中共享和使用，从而实现状态管理。
export const userUserStore=defineStore('user',{
  //定义存储的变量
  state:()=>({
    user:null,
  }),
  actions:{//写函数
    login(user)
    {
      this.user=user;
    },logout(){
      this.user=null;
    }
  }
})
