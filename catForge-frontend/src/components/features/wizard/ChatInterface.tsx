import { useRef, useEffect, useState } from 'react';
import { cn } from '@/utils/cn';
import { Button, Spinner } from '@/components/ui';

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

interface ChatInterfaceProps {
  messages: ChatMessage[];
  onSendMessage: (content: string) => void;
  isLoading?: boolean;
  disabled?: boolean;
}

export function ChatInterface({
  messages,
  onSendMessage,
  isLoading = false,
  disabled = false,
}: ChatInterfaceProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [input, setInput] = useState('');

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim() && !isLoading && !disabled) {
      onSendMessage(input.trim());
      setInput('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="flex flex-col h-[400px] border border-neutral-200 rounded-lg overflow-hidden">
      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-neutral-50">
        {messages.map((message) => (
          <div
            key={message.id}
            className={cn(
              'flex',
              message.role === 'user' ? 'justify-end' : 'justify-start'
            )}
          >
            <div
              className={cn(
                'max-w-[80%] rounded-lg px-4 py-2',
                message.role === 'user'
                  ? 'bg-pastel-blue text-neutral-800'
                  : 'bg-white border border-neutral-200 text-neutral-700'
              )}
            >
              <p className="text-sm whitespace-pre-wrap">{message.content}</p>
              <p className="text-xs text-neutral-500 mt-1">
                {message.timestamp.toLocaleTimeString('de-DE', {
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </p>
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="flex justify-start">
            <div className="bg-white border border-neutral-200 rounded-lg px-4 py-3">
              <div className="flex items-center gap-2">
                <Spinner size="sm" />
                <span className="text-sm text-neutral-500">Generiere Layout...</span>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <form onSubmit={handleSubmit} className="border-t border-neutral-200 p-3 bg-white">
        <div className="flex gap-2">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Nachricht eingeben..."
            disabled={disabled || isLoading}
            rows={1}
            className={cn(
              'flex-1 px-3 py-2 border border-neutral-200 rounded-lg resize-none',
              'focus:outline-none focus:ring-2 focus:ring-pastel-blue focus:border-transparent',
              'placeholder:text-neutral-400',
              (disabled || isLoading) && 'bg-neutral-50 cursor-not-allowed'
            )}
          />
          <Button
            type="submit"
            disabled={!input.trim() || isLoading || disabled}
          >
            Senden
          </Button>
        </div>
        <p className="text-xs text-neutral-500 mt-2">
          Enter zum Senden, Shift+Enter für neue Zeile
        </p>
      </form>
    </div>
  );
}
