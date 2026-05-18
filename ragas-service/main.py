from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import os
import logging
import asyncio
import math
asyncio.set_event_loop_policy(asyncio.DefaultEventLoopPolicy())

from datasets import Dataset
from ragas import evaluate
from ragas.metrics import Faithfulness, AnswerRelevancy
from langchain_groq import ChatGroq
from langchain_huggingface import HuggingFaceEmbeddings
from ragas.llms import LangchainLLMWrapper
from ragas.embeddings import LangchainEmbeddingsWrapper

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="RAGAS Evaluation Service", version="1.0.0")

groq_llm = LangchainLLMWrapper(ChatGroq(
    model="llama-3.1-8b-instant",
    api_key=os.environ["GROQ_API_KEY"]
))

embeddings = LangchainEmbeddingsWrapper(HuggingFaceEmbeddings(
    model_name="sentence-transformers/all-MiniLM-L6-v2"
))

faithfulness = Faithfulness(llm=groq_llm)
answer_relevancy = AnswerRelevancy(llm=groq_llm, embeddings=embeddings)

class EvaluationRequest(BaseModel):
    questions: List[str]
    answers: List[str]
    contexts: List[List[str]]
    ground_truths: Optional[List[str]] = None

class MetricScores(BaseModel):
    faithfulness: Optional[float] = None
    answer_relevancy: Optional[float] = None
    ragas_score: Optional[float] = None

class EvaluationResponse(BaseModel):
    scores: MetricScores
    per_question: List[dict]
    sample_count: int
    status: str

@app.get("/health")
def health():
    return {"status": "ok", "service": "ragas-evaluator"}

def safe_score(val):
    if val is None:
        return None
    try:
        return None if math.isnan(val) or math.isinf(val) else val
    except:
        return None

@app.post("/evaluate", response_model=EvaluationResponse)
async def evaluate_rag(request: EvaluationRequest):
    if not (len(request.questions) == len(request.answers) == len(request.contexts)):
        raise HTTPException(400, "questions, answers, contexts must have same length")

    try:
        data = {
            "question": request.questions,
            "answer": request.answers,
            "contexts": request.contexts,
        }
        if request.ground_truths:
            data["ground_truth"] = request.ground_truths

        dataset = Dataset.from_dict(data)
        metrics = [faithfulness, answer_relevancy]

        result = evaluate(dataset, metrics=metrics)

        scores_dict = result.to_pandas().mean(numeric_only=True).to_dict()
        per_q = result.to_pandas().fillna(0).replace([float('inf'), float('-inf')], 0).to_dict(orient="records")

        f = safe_score(scores_dict.get("faithfulness"))
        ar = safe_score(scores_dict.get("answer_relevancy"))

        vals = [v for v in [f, ar] if v is not None and v > 0]
        ragas_score = len(vals) / sum(1/v for v in vals) if vals else None

        return EvaluationResponse(
            scores=MetricScores(
                faithfulness=f,
                answer_relevancy=ar,
                ragas_score=ragas_score,
            ),
            per_question=per_q,
            sample_count=len(request.questions),
            status="success"
        )

    except Exception as e:
        logger.error(f"Evaluation failed: {e}")
        raise HTTPException(500, f"Evaluation error: {str(e)}")
        
