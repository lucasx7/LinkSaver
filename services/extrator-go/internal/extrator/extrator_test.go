package extrator

import (
	"context"
	"errors"
	"io"
	"strings"
	"testing"
	"time"
)

type buscadorFalso struct {
	fn func(ctx context.Context, rawURL string) (io.ReadCloser, error)
}

func (b buscadorFalso) Buscar(ctx context.Context, rawURL string) (io.ReadCloser, error) {
	return b.fn(ctx, rawURL)
}

func TestExtrairURLInvalida(t *testing.T) {
	ext, err := NovoExtrator(buscadorFalso{fn: func(context.Context, string) (io.ReadCloser, error) {
		return nil, nil
	}})
	if err != nil {
		t.Fatal(err)
	}
	for _, ruim := range []string{"", "ftp://exemplo.com", "sem-esquema"} {
		if _, err := ext.Extrair(context.Background(), ruim); !errors.Is(err, ErrURLInvalida) {
			t.Fatalf("url %q: esperado ErrURLInvalida, veio %v", ruim, err)
		}
	}
}

func TestExtrairTituloEFavicon(t *testing.T) {
	html := `<html><head><title>SIGAA &amp; UFRN</title><link rel="icon" href="/img/ico.png"></head></html>`
	ext, _ := NovoExtrator(buscadorFalso{fn: func(context.Context, string) (io.ReadCloser, error) {
		return io.NopCloser(strings.NewReader(html)), nil
	}})
	meta, err := ext.Extrair(context.Background(), "https://ufrn.br/curso")
	if err != nil || meta.Titulo != "SIGAA & UFRN" || meta.FaviconURL != "https://ufrn.br/img/ico.png" {
		t.Fatalf("resultado inesperado: meta=%+v err=%v", meta, err)
	}
}

func TestExtrairSemTagIconUsaPadrao(t *testing.T) {
	ext, _ := NovoExtrator(buscadorFalso{fn: func(context.Context, string) (io.ReadCloser, error) {
		return io.NopCloser(strings.NewReader("<title>Exemplo</title>")), nil
	}})
	meta, err := ext.Extrair(context.Background(), "https://exemplo.com/pagina")
	if err != nil || meta.FaviconURL != "https://exemplo.com/favicon.ico" {
		t.Fatalf("esperado favicon padrao, veio meta=%+v err=%v", meta, err)
	}
}

func TestExtrairRespeitaPrazoDoContexto(t *testing.T) {
	ext, _ := NovoExtrator(buscadorFalso{fn: func(ctx context.Context, _ string) (io.ReadCloser, error) {
		<-ctx.Done()
		return nil, ctx.Err()
	}})
	ctx, cancel := context.WithTimeout(context.Background(), 20*time.Millisecond)
	defer cancel()
	if _, err := ext.Extrair(ctx, "https://lento.com"); !errors.Is(err, context.DeadlineExceeded) {
		t.Fatalf("esperado DeadlineExceeded, veio %v", err)
	}
}
