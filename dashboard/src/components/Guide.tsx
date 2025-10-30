import { X, Info } from 'lucide-react';

interface GuideProps {
  title: string;
  isGuideOpen: boolean;
  setIsGuideOpen: (isOpen: boolean) => void;
  children: React.ReactNode;
}

export default function Guide({ title, isGuideOpen, setIsGuideOpen, children }: GuideProps) {
  if (!isGuideOpen) {
    return (
      <div className="w-full flex justify-end">
        <button
          onClick={() => setIsGuideOpen(true)}
          className="relative z-30 bg-white rounded-full p-2 shadow-md hover:bg-slate-100 transition-colors"
          title="Open guide"
        >
          <Info size={20} className="text-slate-800" />
        </button>
      </div>
    );
  }

  return (
    <div className="w-full max-h-full bg-white rounded-lg shadow-lg text-slate-800 relative z-30 flex flex-col">
      <div className="p-4 border-b border-slate-200">
        <button
          onClick={() => setIsGuideOpen(false)}
          className="absolute top-2 right-2 text-slate-500 hover:text-slate-800"
          title="Close guide"
        >
          <X size={25} />
        </button>
        <h2 className="text-xl font-bold">{title}</h2>
      </div>
      <div className="overflow-y-auto p-4">
        <div className="space-y-3 text-sm">
          {children}
        </div>
      </div>
    </div>
  );
}
