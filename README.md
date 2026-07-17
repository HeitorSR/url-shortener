# URL Shortener

API REST para encurtamento de URLs, desenvolvida com Java 8, JAX-RS e WildFly 10. A aplicação permite criar URLs encurtadas com alias informado ou gerado automaticamente, consultar os registros, editar e excluir URLs, redirecionar por alias e remover registros expirados.

Os dados são armazenados no banco H2. A expiração das URLs é determinada pela configuração da aplicação.

## Tecnologias

- Java 8
- JAX-RS / RESTEasy
- WildFly 10.1.0.Final
- Maven
- H2 Database
- JUnit 4 e REST Assured para testes de integração

## Pré-requisitos

Para executar o projeto, instale:

- JDK 8;
- Maven 3;
- WildFly 10.1.0.Final;
- Git, caso o projeto seja clonado do repositório;
- IntelliJ IDEA, opcionalmente.

Confirme as instalações no terminal:

```bash
java -version
mvn -version
```

O Maven deve indicar que está utilizando o Java 8.


Substitua `URL_DO_REPOSITORIO` e `NOME_DO_REPOSITORIO` pelos dados reais do repositório.

## Gerando o WAR

Na raiz do projeto, onde está o `pom.xml`, execute:

```bash
mvn clean package
```

O Maven criará o WAR dentro de `target`. Considerando o nome configurado neste projeto, o arquivo esperado é:

```text
target/url-shortener.war
```

## Executando no WildFly

### Deploy manual no Windows

1. Copie `target/url-shortener.war` para:

   ```text
   WILDFLY_HOME\standalone\deployments\
   ```

2. Inicie o WildFly:

   ```powershell
   WILDFLY_HOME\bin\standalone.bat
   ```

3. Aguarde o log confirmar o deploy:

   ```text
   Deployed "url-shortener.war"
   ```

4. A aplicação estará disponível em:

   ```text
   http://localhost:8080/url-shortener
   ```

Se o WAR tiver outro nome, o contexto normalmente também terá outro nome. Consulte a mensagem de deploy do WildFly ou configure `<finalName>url-shortener</finalName>` no `pom.xml`.

### Execução pelo IntelliJ IDEA

1. Abra o projeto pelo `pom.xml`.
2. Configure o Project SDK como Java 8.
3. Acesse **Run > Edit Configurations**.
4. Adicione uma configuração **JBoss Server > Local**.
5. Informe o diretório do WildFly 10.1.0.Final.
6. Na aba **Deployment**, adicione o artifact `url-shortener:war` ou `url-shortener:war exploded`.
7. Inicie a configuração criada.

## Interface HTML

Se o arquivo estiver em `src/main/webapp/index.html`, a interface para testar os endpoints poderá ser acessada em:

```text
http://localhost:8080/url-shortener/
```

Servir o HTML pelo mesmo WAR evita problemas de CORS entre a página e a API.

## Endereço base da API

Os exemplos abaixo consideram:

```text
http://localhost:8080/url-shortener/api
```

Essa URL é formada por:

- contexto do WAR: `/url-shortener`;
- caminho JAX-RS: `/api`;
- recurso de URLs: `/urls`.

Caso as anotações `@ApplicationPath` ou `@Path` estejam diferentes no código, ajuste os exemplos conforme necessário.

## Endpoints

### Criar uma URL encurtada

```http
POST /api/urls
Content-Type: application/json
```

Com alias:

```json
{
  "url": "https://www.example.com/produtos/123",
  "alias": "produto-123"
}
```

Exemplo com `curl`:

```bash
curl -i -X POST "http://localhost:8080/url-shortener/api/urls" \
  -H "Content-Type: application/json" \
  -d "{\"url\":\"https://www.example.com/produtos/123\",\"alias\":\"produto-123\"}"
```

O alias é opcional. Quando não for enviado, a aplicação gera um valor automaticamente:

```bash
curl -i -X POST "http://localhost:8080/url-shortener/api/urls" \
  -H "Content-Type: application/json" \
  -d "{\"url\":\"https://www.example.com/produtos/123\"}"
```

### Redirecionar pela URL encurtada

```http
GET /api/urls/{alias}
```

Exemplo:

```bash
curl -i "http://localhost:8080/url-shortener/api/urls/produto-123"
```

Para fazer o `curl` acompanhar o redirecionamento, utilize `-L`:

```bash
curl -i -L "http://localhost:8080/url-shortener/api/urls/produto-123"
```

No navegador, basta acessar:

```text
http://localhost:8080/url-shortener/api/urls/produto-123
```

### Listar todas as URLs

```http
GET /api/urls
```

Exemplo:

```bash
curl -i "http://localhost:8080/url-shortener/api/urls"
```

### Editar uma URL

```http
PUT /api/urls/{alias}
Content-Type: application/json
```

Exemplo:

```bash
curl -i -X PUT "http://localhost:8080/url-shortener/api/urls/produto-123" \
  -H "Content-Type: application/json" \
  -d "{\"url\":\"https://www.example.com/produtos/456\"}"
```

### Excluir uma URL

```http
DELETE /api/urls/{alias}
```

Exemplo:

```bash
curl -i -X DELETE "http://localhost:8080/url-shortener/api/urls/produto-123"
```

### Excluir URLs expiradas

```http
DELETE /api/urls/expired
```

Exemplo:

```bash
curl -i -X DELETE "http://localhost:8080/url-shortener/api/urls/expired"
```

Esse endpoint remove os registros cuja data de criação ultrapassou o período de validade configurado na aplicação.

## Resumo dos endpoints

| Método | Caminho | Descrição |
| --- | --- | --- |
| `POST` | `/api/urls` | Cria uma URL encurtada |
| `GET` | `/api/urls/{alias}` | Redireciona para a URL original |
| `GET` | `/api/urls` | Lista as URLs cadastradas |
| `PUT` | `/api/urls/{alias}` | Atualiza a URL original |
| `DELETE` | `/api/urls/{alias}` | Exclui uma URL pelo alias |
| `DELETE` | `/api/urls/expired` | Exclui todas as URLs expiradas |

## Banco H2

O projeto utiliza H2 para simplificar a execução local. Se estiver configurado no modo em memória, os dados existirão somente enquanto a aplicação estiver em execução e serão apagados quando o WildFly for encerrado ou reiniciado.

Esse comportamento é adequado para desenvolvimento, demonstrações e testes. Para uso em produção, recomenda-se utilizar um banco persistente, como PostgreSQL, e configurar um DataSource no servidor de aplicação.

## Testes

Os testes de integração exercitam a aplicação implantada no WildFly. Antes de executá-los:

1. Inicie o WildFly.
2. Confirme que o WAR foi implantado corretamente.
3. Confirme que a aplicação responde em `http://localhost:8080/url-shortener`.

Em seguida, execute:

```bash
mvn verify
```

Os relatórios do Maven Failsafe são gerados em:

```text
target/failsafe-reports
```

## Observações

- O H2 em memória perde os dados após uma reinicialização.
- Se nenhum alias for enviado na criação, a aplicação gera um automaticamente.
- O processo de remoção de expirados utiliza a data de criação e o período de validade configurado.
