from flask import Flask
from flask import request
import json
import re

app = Flask(__name__)

@app.route("/validate-iban")
def validate_iban():
    return json.dumps({
        "iban_is_valid": len(re.findall("[A-Z]{2}[0-9]{20}", str(request.args.get('iban')))) == 1
    })