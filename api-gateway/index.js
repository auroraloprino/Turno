const express = require('express')
const { createProxyMiddleware } = require('http-proxy-middleware')

const app = express()

const AUTH_URL      = process.env.AUTH_SERVICE_URL      || 'http://localhost:8081'
const TIMBRATURA_URL = process.env.TIMBRATURA_SERVICE_URL || 'http://localhost:8082'
const PERMESSI_URL  = process.env.PERMESSI_SERVICE_URL  || 'http://localhost:8083'
const TURNI_URL     = process.env.TURNI_SERVICE_URL     || 'http://localhost:8084'
const CHAT_URL      = process.env.CHAT_SERVICE_URL      || 'http://localhost:8085'

const proxy = (target) => createProxyMiddleware({ target, changeOrigin: true })

app.use('/api/auth',       proxy(AUTH_URL))
app.use('/api/users',      proxy(AUTH_URL))
app.use('/api/timbrature', proxy(TIMBRATURA_URL))
app.use('/api/permessi',   proxy(PERMESSI_URL))
app.use('/api/turni',      proxy(TURNI_URL))
app.use('/api/chat',       proxy(CHAT_URL))
app.use('/ws',             createProxyMiddleware({
  target: CHAT_URL,
  changeOrigin: true,
  ws: true,
}))

app.listen(8080, () => console.log('Gateway running on :8080'))
