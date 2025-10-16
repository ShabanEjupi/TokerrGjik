const express = require('express');

const app = express();

app.get('/', (req, res) => {
  res.send(`
    <h1>Hello from the UP!</h1>
    <p>I dare to send a request to /drop</p>
  `);
});

app.get('/drop', (req, res) => {
  process.exit(1);
});

app.listen(9876);
