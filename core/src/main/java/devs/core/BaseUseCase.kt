package devs.core
interface BaseUseCase<Input, Output> {
    suspend fun process(params: Input): Output
}