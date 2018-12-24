const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const apiMocker = require('connect-api-mocker');

// change the server path to your server location path
const exoServerPath = "../../../exo-servers/platform-5.2.x-create-event-SNAPSHOT/";

let config = merge(webpackCommonConfig, {
  mode: 'development',
  output: {
    path: exoServerPath + 'webapps/calendar/'
  },
  devtool: 'source-map'
});

config.plugins.push(new CopyWebpackPlugin([{from: 'src/main/webapp/lang/*.json', to: './lang', flatten: true}]));

module.exports = config;
