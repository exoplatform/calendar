const path = require('path');
const ExtractTextWebpackPlugin = require('extract-text-webpack-plugin');

let config = {
  context: path.resolve(__dirname, './'),
  entry: {
    calendar: './src/main/webapp/vue-app/main.js'
  },
  output: {
    filename: 'vue-app/js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  module: {
    rules: [
      {
        test: /\.less$/,
        use: ExtractTextWebpackPlugin.extract({
          fallback: 'vue-style-loader',
          use: [
            {
              loader: 'css-loader',
              options: {
                sourceMap: true
              }
            },
            {
              loader: 'less-loader',
              options: {
                sourceMap: true
              }
            }
          ]
        })
      },
      {
        test: /\.css$/,
        use: ['vue-style-loader', 'css-loader']
      },
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
          'eslint-loader'
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader'
        ]
      }
    ]
  },
  plugins: [
    new ExtractTextWebpackPlugin('vue-app/css/[name].css')
  ]
};

module.exports = config;