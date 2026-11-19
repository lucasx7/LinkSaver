package grpcserver

import (
	"context"
	"io"
	"net"
	"strings"
	"testing"

	metadadosv1 "github.com/lucasx7/LinkSaver/services/extrator-go/gen/linksaver/metadados/v1"
	"github.com/lucasx7/LinkSaver/services/extrator-go/internal/extrator"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	healthpb "google.golang.org/grpc/health/grpc_health_v1"
	"google.golang.org/grpc/status"
	"google.golang.org/grpc/test/bufconn"
)

type buscadorTeste string

func (b buscadorTeste) Buscar(context.Context, string) (io.ReadCloser, error) {
	return io.NopCloser(strings.NewReader(string(b))), nil
}

func novoClienteBufconn(t *testing.T, html string) (*grpc.ClientConn, func()) {
	t.Helper()
	lis := bufconn.Listen(1024 * 1024)
	ext, _ := extrator.NovoExtrator(buscadorTeste(html))
	srv := grpc.NewServer()
	RegistrarServicos(srv, ext)
	go srv.Serve(lis)
	conn, err := grpc.NewClient("passthrough:///bufnet",
		grpc.WithContextDialer(func(context.Context, string) (net.Conn, error) { return lis.Dial() }),
		grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		t.Fatal(err)
	}
	return conn, func() { conn.Close(); srv.Stop() }
}

func TestServidorGRPC_ExtrairEHealth(t *testing.T) {
	conn, fechar := novoClienteBufconn(t, "<title>LinkSaver</title>")
	defer fechar()
	cli := metadadosv1.NewMetadadosServiceClient(conn)
	resp, err := cli.ExtrairMetadados(context.Background(), &metadadosv1.ExtrairMetadadosRequest{Url: "https://ufrn.br"})
	if err != nil || resp.GetTitulo() != "LinkSaver" {
		t.Fatalf("esperado LinkSaver, veio resp=%v err=%v", resp, err)
	}
	if _, err := cli.ExtrairMetadados(context.Background(), &metadadosv1.ExtrairMetadadosRequest{Url: "ruim"}); status.Code(err) != codes.InvalidArgument {
		t.Fatalf("esperado InvalidArgument, veio %v", err)
	}
	h, err := healthpb.NewHealthClient(conn).Check(context.Background(), &healthpb.HealthCheckRequest{})
	if err != nil || h.GetStatus() != healthpb.HealthCheckResponse_SERVING {
		t.Fatalf("esperado SERVING, veio %v err=%v", h, err)
	}
}
