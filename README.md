# Lua HTTPS

## Compile on MacOS


```sh
cmake -Bbuild -S. -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=$PWD/install -DCMAKE_CXX_FLAGS="-fobjc-arc -std=c++11 -stdlib=libc++" cmake --build build --target install
```

