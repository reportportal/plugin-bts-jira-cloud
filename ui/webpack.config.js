const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const ModuleFederationPlugin = require('webpack/lib/container/ModuleFederationPlugin');
const pjson = require('./package.json');

const config = {
  entry: ['./src/index.js'],
  output: {
    path: path.resolve(__dirname, 'build/public'),
    filename: 'main.js',
    libraryTarget: 'umd',
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        use: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.svg$/,
        loader: 'svg-inline-loader',
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.jsx'],
    alias: {
      components: path.resolve(__dirname, 'src/components'),
      constants: path.resolve(__dirname, 'src/constants'),
      icons: path.resolve(__dirname, 'src/icons'),
      hooks: path.resolve(__dirname, 'src/hooks'),
      utils: path.resolve(__dirname, 'src/utils'),
    },
  },
  plugins: [
    new ModuleFederationPlugin({
      name: 'JIRA_Cloud',
      filename: `remoteEntity.js`,
      shared: {
        react: {
          import: 'react',
          shareKey: 'react',
          shareScope: 'default',
          singleton: true,
          requiredVersion: pjson.dependencies.react,
        },
        'react-dom': {
          singleton: true,
          requiredVersion: pjson.dependencies['react-dom'],
        },
        'react-redux': {
          singleton: true,
          requiredVersion: pjson.dependencies['react-redux'],
        },
        'redux-form': {
          singleton: true,
          requiredVersion: pjson.dependencies['redux-form'],
        },
        'react-tracking': {
          singleton: true,
          requiredVersion: pjson.dependencies['react-tracking'],
        },
      },
      exposes: {
        './integrationSettings': './src/components/integrationSettings',
        './integrationFormFields': './src/components/integrationFormFields',
      },
    }),
    new CopyPlugin({
      patterns: [
        { from: path.resolve(__dirname, './src/metadata.json') },
        { from: path.resolve(__dirname, './src/plugin-icon.svg') },
      ],
    }),
  ],
};

module.exports = config;
