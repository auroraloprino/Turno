const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')

const app = express()

const AUTH_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:8081'
const TIMBRATURA_URL = process.env.TIMBRATURA_SERVICE_URL || 'http://localhost:8082'
const PERMESSI_URL = process.env.PERMESSI_SERVICE_URL || 'http://localhost:8083'
const TURNI_URL = process.env.TURNI_SERVICE_URL || 'http://localhost:8084'
const CHAT_URL = process.env.CHAT_SERVICE_URL || 'http://localhost:8085'

// niente prefisso in app.use, altrimenti Express lo toglierebbe dal path 
const proxy = (pathFilter, target, extra = {}) =>
  createProxyMiddleware({ pathFilter, target, changeOrigin: true, ...extra })

app.use(proxy(['/api/auth', '/api/users'], AUTH_URL))
app.use(proxy('/api/timbrature', TIMBRATURA_URL))
app.use(proxy('/api/permessi', PERMESSI_URL))
app.use(proxy('/api/turni', TURNI_URL))
app.use(proxy('/api/chat', CHAT_URL))
app.use(proxy('/ws', CHAT_URL, { ws: true }))

app.listen(8080, () => console.log('Gateway running on :8080'))
