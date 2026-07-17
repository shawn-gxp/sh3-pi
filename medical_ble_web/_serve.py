import uvicorn
uvicorn.run("app:app", host="127.0.0.1", port=8741, reload=False, log_level="info")
