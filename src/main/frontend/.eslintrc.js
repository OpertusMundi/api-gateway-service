module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    'plugin:vue/essential',
    '@vue/airbnb',
    '@vue/typescript/recommended',
  ],
  parserOptions: {
    ecmaVersion: 2020,
  },
  rules: {
    /**
     * Configure airbnb eslint rules for vue.js component lifecycle methods
     *
     * See; https://github.com/vuejs/vue-cli/issues/1203#issuecomment-452471826
     */
    'class-methods-use-this': 0,
    // 'class-methods-use-this': ['error', {
    //   exceptMethods: [
    //     'mounted',
    //   ],
    // }],
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'max-len': ["error", { "code": 5000 }],
    '@typescript-eslint/no-explicit-any': 'off',
    '@typescript-eslint/camelcase': 'off',
    // Uncomment if using windows. Git client should be configured to handle
    // line breaks i.e. git config --global core.autocrlf true
    // 'linebreak-style': 'warn',
  },
  overrides: [
    {
      files: [
        '**/__tests__/*.{j,t}s?(x)',
        '**/tests/unit/**/*.spec.{j,t}s?(x)',
      ],
      env: {
        jest: true,
      },
    },
  ],
};
