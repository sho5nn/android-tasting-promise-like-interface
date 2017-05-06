This repository is sample code when assuming asynchronous processing to be done on Android by an interface like method chain of JavaScript Promise.
> このリポジトリは、JavaScript PromiseのメソッドチェーンのようなインタフェースでAndroid上で非同期処理を行うと仮定した場合のサンプルコードです。

However, if you are in an environment where you can use RxJava etc., you probably do not have much opportunity to use this sample code.
> しかしながら [RxJava](https://github.com/ReactiveX/RxJava) などを使うことができる環境の中にあなたが居るなら、このサンプルコードを活用する機会はあまり無いでしょう。

If you can not use ( rather **Not available/permitted/allowed to use** ) useful libraries like RxJava but if you want to write asynchronous processing chains comfortably, please refer to this sample code.
> RxJava のような便利なライブラリを使わない（ むしろ **使える状況でない** ）けど非同期処理のチェーンを楽に記述したいなら、このサンプルコードを参考にしてください。

This sample code does not use the Lambda, but if your environment permits it, you can use it.
> このサンプルコードはラムダ式を使用していませんが、あなたの環境が許すのであればそれを使用してもいいでしょう。

Reference...

- [Promise - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise)
- [jdeferred/jdeferred: Java Deferred/Promise library similar to JQuery.](https://github.com/jdeferred/jdeferred)

# Assuming

Asynchronous processing uses the `java.util.concurrent` package. At the worker thread, execute a `Runnable` or `Callable` interface object that implements asynchronous processing.
>非同期処理は `java.util.concurrent` ユーティリティパッケージを用いています。ワーカースレッドで、非同期処理を実装した `Runnable` もしくは `Callable` インターフェースオブジェクトを実行しています。

# Feature

It generates one Promise object for one asynchronous processing. You can describe that Promise object like a chain.
> 1 つの非同期処理に対して Promise オブジェクトを 1 つ生成します。その Promise オブジェクトをチェーンのように記述することができます。

Finally, by calling the `Promise.done()` , asynchronous processing of the first Promise object is executed, and the connected Promise objects are sequentially executed.
> 最後に `Promise.done()` を呼び出すことで、最初の Promise オブジェクトの非同期処理が実行され、繋げた Promise オブジェクトが順次実行されていきます。

```java
Promise
  .when(executor, Promise.single(new FooTask()))
  .then(new FulfillCallbackThenAll<String>() {
    @Override
    public PromiseTask.All onFulfilled(String value) {
      return Promise.all(new BarTask(), new BazTask());
    }
  })
  .then(new FulfillCallbackThenSingle<Object[], Void>() {
    @Override
    public PromiseTask.Single<Void> onFulfilled(Object[] value) {
      return Promise.single(new QuxTask());
    }
  }, new RejectCallbackDone<Throwable[]>() {
    @Override
    public void onRejected(Throwable[] value) {
      Toast.makeText(SimpleActivity.this, "failure", Toast.LENGTH_SHORT).show();
      progress.setVisibility(View.GONE);
    }
  }).atMain()
  .done(new FulfillCallbackDone<Void>() {
    @Override
    public void onFulfilled(Void value) {
      progress.setVisibility(View.GONE);
      updateViewWhenSuccess();
    }
  }, new RejectCallbackDone<Throwable>() {
    @Override
    public void onRejected(Throwable value) {
      progress.setVisibility(View.GONE);
      updateViewWhenFailure();
    }
  }).atMain();
```

with Lambda.

```java
Promise
  .when(executor, Promise.single(new FooTask()))
  .then((FulfillCallbackThenAll<String>) value -> Promise.all(new BarTask(), new BazTask()))
  .then(
    (FulfillCallbackThenSingle<Object[], Void>) value -> {
      return Promise.single(new QuxTask());
    },
    (RejectCallbackDone<Throwable[]>) value -> {
      Toast.makeText(SimpleActivity.this, "failure", Toast.LENGTH_SHORT).show();
      progress.setVisibility(View.GONE);
    }).atMain()
  .done(
    value -> {
      progress.setVisibility(View.GONE);
      updateViewWhenSuccess();
    },
    value -> {
      progress.setVisibility(View.GONE);
      updateViewWhenFailure();
    }).atMain();
```

You can choose from either main or worker thread to callback.

- `Promise.atMain()`
- `Promise.at(Handler)`

## Promise.single()

```java
Callable<Integer> callable = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    return 1;
    // or
    // throw new FooException("because...");
  }
};

Promise
  .when(executor, Promise.single(callable))
  .done(new FulfillCallbackDone<Integer>() {
    @Override
    public void onFulfilled(Integer value) {
      Log.d(TAG, value); // 1
    }
  }, new RejectCallbackDone<Throwable>() {
    @Override
    public void onRejected(Throwable value) {
      Log.w(TAG, value); // ExecutionException
      Log.w(TAG, value.getCause()); // FooException
    }
  });
```

like a JavaScript Promise `Promise.prototype.then()`

- [Promise.prototype.then() - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/then)

```js
var p1 = new Promise(function(resolve, reject) {
  resolve("Success!");
  // or
  // reject ("Error!");
});

p1.then(function(value) {
  console.log(value); // Success!
}, function(reason) {
  console.log(reason); // Error!
});
```

## Promise.all()

```java
Callable<Integer> c1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    return 1;
  }
};
Callable<Integer> c2 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    return 1337;
  }
};
Callable<String> c3 = new Callable<String>() {
  @Override
  public String call() throws Exception {
    Thread.sleep(100);
    return "foo";
  }
};

Promise
  .when(executor, Promise.all(c1, c2, c3))
  .done(new FulfillCallbackDone<Object[]>() {
    @Override
    public void onFulfilled(Object[] value) {
      Log.d(TAG, value[0]); // 1
      Log.d(TAG, value[1]); // 1337
      Log.d(TAG, value[2]); // foo
    }
  });
```

like a JavaScript Promise `Promise.all(iterable);`

- [Promise.all() - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all)

```js
var p1 = Promise.resolve(3);
var p2 = 1337;
var p3 = new Promise((resolve, reject) => {
  setTimeout(resolve, 100, 'foo');
});

Promise.all([p1, p2, p3]).then(values => {
  console.log(values); // [3, 1337, "foo"]
});
```

### Promise.all Not fail-fast behaviour

Promise.all is NOT immediately rejected even if any of the elements are rejected. For example, if you passes in one promise that resolve after a timeout and one promise that rejects immediately and one promise that reject after a timeout, then Promise.all will NOT reject immediately.
> Promise.all に渡したいずれかの Promise が reject されても Promise.all はすぐに reject しません。例えば、タイムアウト後に resolve する Promise と、直ちに reject する Promise と、タイムアウト後に reject する Promise を渡しても、 Promise.all は直ちに reject しません。

```java
Callable<Integer> c1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(500);
    return 1;
  }
};

Callable<Integer> e1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    throw new Foo1Exception();
  }
};

Callable<Integer> e2 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(1000);
    throw new Foo2Exception();
  }
};

Promise
  .when(executor, Promise.all(c1, e1, e2))
  .done(new FulfillCallbackDone<Object[]>() {
    @Override
    public void onFulfilled(Object[] value) {
      // not call
    }
  }, new RejectCallbackDone<Throwable[]>() {
    @Override
    public void onRejected(Throwable[] value) {
      Log.w(TAG, value[0]);            // null
      Log.w(TAG, value[1].getCause()); // Foo1Exception
      Log.w(TAG, value[2].getCause()); // Foo2Exception
    }
  });
```

## Promise.race()

```java
Callable<Integer> c1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(300);
    return 1;
  }
};

Callable<Integer> c2 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(200);
    return 2;
  }
};

Callable<Integer> c3 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(100);
    return 3;
  }
};

Promise
  .when(executor, Promise.race(c1, c2, c3))
  .done(new FulfillCallbackDone<Object>() {
    @Override
    public void onFulfilled(Object value) {
      Log.d(TAG, value); // 3
    }
  });
```

```java
Callable<Integer> c1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(300);
    return 1;
  }
};

Callable<Integer> e1 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(200);
    throw new Foo1Exception();
  }
};

Callable<Integer> e2 = new Callable<Integer>() {
  @Override
  public Integer call() throws Exception {
    Thread.sleep(100);
    throw new Foo2Exception();
  }
};

Promise
  .when(executor, Promise.race(c1, e1, e2))
  .done(new FulfillCallbackDone<Object>() {
    @Override
    public void onFulfilled(Object value) {
      // not call
    }
  }, new RejectCallbackDone<Throwable>() {
    @Override
    public void onRejected(Throwable value) {
      Log.w(TAG, value); // Foo2Exception
    }
  });
```

like a JavaScript Promise `Promise.race(iterable);`

- [Promise.race() - JavaScript | MDN](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/race)

```js
var p1 = new Promise(function(resolve, reject) {
    setTimeout(resolve, 500, 'one');
});
var p2 = new Promise(function(resolve, reject) {
    setTimeout(resolve, 100, 'two');
});

Promise.race([p1, p2]).then(function(value) {
  console.log(value); // "two"
  // Both resolve, but p2 is faster
});

var p3 = new Promise(function(resolve, reject) {
    setTimeout(resolve, 100, 'three');
});
var p4 = new Promise(function(resolve, reject) {
    setTimeout(reject, 500, 'four');
});

Promise.race([p3, p4]).then(function(value) {
  console.log(value); // "three"
  // p3 is faster, so it resolves
}, function(reason) {
  // Not called
});

var p5 = new Promise(function(resolve, reject) {
    setTimeout(resolve, 500, 'five');
});
var p6 = new Promise(function(resolve, reject) {
    setTimeout(reject, 100, 'six');
});

Promise.race([p5, p6]).then(function(value) {
  // Not called
}, function(reason) {
  console.log(reason); // "six"
  // p6 is faster, so it rejects
});
```

## Not implemented

- promise cancel
- timeout

## License

MIT