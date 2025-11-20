import os
import os.path as osp


BASE_DIR = osp.dirname(__file__)
DATA_DIR = os.getenv('RAG_DATA_DIR', osp.join(BASE_DIR, 'data', 'kb'))
INDEX_PATH = os.getenv('RAG_INDEX_PATH', osp.join(DATA_DIR, 'index.faiss'))
META_PATH = os.getenv('RAG_META_PATH', osp.join(DATA_DIR, 'meta.json'))

MODEL_NAME = os.getenv('RAG_MODEL_NAME', 'BAAI/bge-small-zh-v1.5')

DB_HOST = os.getenv('RAG_DB_HOST', 'localhost')
DB_PORT = int(os.getenv('RAG_DB_PORT', '3306'))
DB_USER = os.getenv('RAG_DB_USER', 'root')
DB_PASS = os.getenv('RAG_DB_PASS', 'root')
DB_NAME = os.getenv('RAG_DB_NAME', 'mental_health')