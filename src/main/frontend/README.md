# Vue.js Client for OpertusMundi

## Vue CLI

Node Version Requirement:

Vue CLI requires Node.js version [8.9](https://nodejs.org/en/) or above (8.11.0+ recommended). Since the project is built for production
using the maven frontend plugin, Node.js version `10.16.3` must be used. Multiple versions of Node may be installed using
[nvm](https://github.com/nvm-sh/nvm).

Install Vue CLI:

```
npm install -g @vue/cli
```

## Development configuration

In order to configure the project for development, the file `.env.development` must be created at the project root folder (`package.json` location). Edit the contents of the file and add the following lines:

```
VUE_APP_API_GATEWAY_URL=https://api.dev.opertusmundi.eu
VUE_APP_SERVER_PORT=8080
```

For additional information see [.env.production](.env.production).

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Run your unit tests
```
npm run test:unit
```

### Run your end-to-end tests
```
npm run test:e2e
```

### Lints and fixes files
```
npm run lint
```