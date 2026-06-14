var defaultTarget = 'http://localhost:8080';
module.exports = [
    {
        context: ['/nbts/api/**'],
        target: defaultTarget,
        changeOrigin: true,
    },
];
