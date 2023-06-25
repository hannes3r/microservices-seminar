cd rest-python
. env/bin/activate
gunicorn -w 1 -b 0.0.0.0:9502 main:app