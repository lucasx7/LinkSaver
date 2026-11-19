package httpbuscador

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"time"
)

type BuscadorHTTP struct{ cliente *http.Client }

func NovoBuscadorHTTP(timeout time.Duration) *BuscadorHTTP {
	return &BuscadorHTTP{cliente: &http.Client{Timeout: timeout}}
}

func (b *BuscadorHTTP) Buscar(ctx context.Context, rawURL string) (io.ReadCloser, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, rawURL, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("User-Agent", "LinkSaver-Extrator/1.0")
	resp, err := b.cliente.Do(req)
	if err != nil {
		return nil, err
	}
	if resp.StatusCode >= http.StatusBadRequest {
		resp.Body.Close()
		return nil, fmt.Errorf("status http %d", resp.StatusCode)
	}
	return resp.Body, nil
}
