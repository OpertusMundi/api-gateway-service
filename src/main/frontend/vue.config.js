module.exports = {
  /*
   * Move all assets under a single directory
   */
  assetsDir: 'assets',
  /*
   * Update output directory for production build to add static assets to the final jar/war file
   */
  outputDir: process.env.NODE_ENV === 'development' ? 'dist' : '../resources/public',
  /*
   * For production build, index.html is treated as a Spring Thymeleaf template
   *
   * https://www.thymeleaf.org/documentation.html
   */
  indexPath: process.env.NODE_ENV === 'development' ? 'index.html' : '../templates/index.html',
  /* For development, proxy all action requests
   *
   * https://github.com/chimurai/http-proxy-middleware#http-proxy-options
   */
  devServer: {
    port: process.env.VUE_APP_SERVER_PORT,
    proxy: process.env.NODE_ENV === 'development' ? {
      '^/action': {
        target: process.env.VUE_APP_API_GATEWAY_URL,
        ws: true,
        changeOrigin: true,
        cookieDomainRewrite: '',
        autoRewrite: true,
      },
      '^/(login|logged-in|logout|logged-out|login/opertus-mundi|error/*)': {
        target: process.env.VUE_APP_API_GATEWAY_URL,
        ws: true,
        changeOrigin: true,
        cookieDomainRewrite: '',
        /*
         * Rewrites the location host/port on (301/302/307/308) redirects based on requested host/port. Default: false.
         * Required for authentication logged-in redirect.
         */
        autoRewrite: true,
        /*
         * Rewrites the location protocol on (301/302/307/308) redirects to 'http' or 'https'. Default: null.
         * Required for authentication logged-out redirect.
         */
        protocolRewrite: 'http',
      },
    } : null,
  },

  css: {
    loaderOptions: {
      sass: {
        prependData: `@import "@/assets/styles/style.scss";`
      }
    }
  },
  // chainWebpack: config => {
  //   config.module
  //     .rule("fonts")
  //     .test(/\.(ttf|otf|eot|woff|woff2)$/)
  //     .use("file-loader")
  //       .loader("file-loader")
  //       .tap(options => {
  //         options = {
  //           // limit: 10000,
  //           name: '/assets/fonts/[name].[ext]',
  //         }
  //         return options
  //       })
  //       .end()
  // }
};
