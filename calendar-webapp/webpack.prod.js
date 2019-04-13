const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(__dirname, './target/calendar/')
  },
  devtool: 'cheap-module-eval-source-map'
});

module.exports = config;