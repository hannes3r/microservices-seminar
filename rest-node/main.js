const express = require('express')
const app = express()
const port = 9501

const blacklist = [
    "Black List",
    "Hannes Dreier"
]

app.get('/check-blacklist', (req, res) => {
  console.log(req.query.name)
  res.send(JSON.stringify({
    is_blacklisted: blacklist.indexOf(req.query.name) == -1 ? false : true
  }))
})

app.listen(port, () => {
  console.log(`Listening on port ${port}`)
})