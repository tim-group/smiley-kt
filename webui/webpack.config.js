const webpack = require("webpack");
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const KotlinWebpackPlugin = require('@jetbrains/kotlin-webpack-plugin');

function relative(suffix) {
    return path.resolve(__dirname, suffix);
}

const production = process.env.NODE_ENV === "production";

module.exports = {
    context: relative("src/main/javascript"),
    entry: ["./index", "kotlinApp"],
    mode: production ? 'production' : 'development',
    output: {
        path: relative('build/site'),
        filename: production ? "[name]-[hash].js" : "[name].js"
    },
    resolve: {
        modules: ["kotlin_build", "node_modules"]
    },
    devtool: production ? "source-map" : "cheap-module-eval-source-map",
    devServer: {
        port: 3000,
        host: "0.0.0.0",
        publicPath: "/",
        disableHostCheck: true,
        proxy: { "/_api": "http://localhost:8000" },
        hot: true
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules|kotlin_build/,
                use: {
                    loader: 'babel-loader'
                }
            },
            {
                test: /\.js$/,
                include: path.resolve(__dirname, "../kotlin_build"),
                exclude: [
                    /kotlin\.js$/
                ],
                use: ['source-map-loader'],
                enforce: 'pre',
            },
            {
                test: /\.css$/,
                use: [ 'style-loader', 'css-loader' ]
            },
            {
                test: /\.(ico|png|woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=100000'
            }
        ]
    },
    plugins: (function() {
        const plugins = [
            new HtmlWebpackPlugin({
                title: "Smiley-KT",
                appVersion: process.env.ORIGINAL_BUILD_NUMBER ? "1.0." + process.env.ORIGINAL_BUILD_NUMBER :
                    process.env.BUILD_NUMBER ? "1.0." + process.env.BUILD_NUMBER : "dev"
            }),
            new KotlinWebpackPlugin({
                src: __dirname + "/src/main/kotlin",
                librariesAutoLookup: true,
                verbose: true,
            }),
            new webpack.optimize.SplitChunksPlugin()
        ];

        if (production) {
            plugins.push(
                new webpack.HashedModuleIdsPlugin(),
                new webpack.NamedChunksPlugin()
            );
        }
        else {
            plugins.push(
                new webpack.HotModuleReplacementPlugin()
            );
        }

        return plugins;
    })()
};
